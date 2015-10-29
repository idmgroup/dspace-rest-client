#!/usr/bin/perl

use strict;

use LWP::Simple;
use Storable qw(dclone);
use XML::LibXML;

my $wadl_file = shift;

sub to_java_type {
    my $xs_type = shift;
    if ($xs_type eq 'xs:string') {
        return 'String';
    } elsif ($xs_type eq 'xs:int') {
        return 'Integer';
    } else {
        die $xs_type;
    }
}

my $xpc = XML::LibXML::XPathContext->new;
$xpc->registerNs('wadl', 'http://wadl.dev.java.net/2009/02');

sub transform_element {
    my $element = shift;
    $element = uc(substr($element, 0, 1)).substr($element, 1, length($element) - 1);
    $element =~ s/Resourcepolicy/ResourcePolicy/g;
    $element =~ s/Metadataentry/MetadataEntry/g;
    return $element;
}

my %custom_return_types;
$custom_return_types{'test'} = 'String';
$custom_return_types{'status'} = 'Status';
$custom_return_types{'login'} = 'String';
$custom_return_types{'logout'} = 'Void';
$custom_return_types{'sayHtmlHello'} = 'String';
$custom_return_types{'getBitstreamData'} = 'byte[]';
$custom_return_types{''} = '';

sub get_return_type {
    my ($method) = @_;
    my $id = $method->getAttribute('id');
    my $type = $custom_return_types{$id};
    if (defined $type) {
        return $type;
    }

    my $id2 = $id;
    $id2 =~ s/By[A-Za-z]+$//;
    my ($prefix, $suffix);
    if (($prefix, $suffix) = $id2 =~ m/^([a-z]+).*([A-Z][a-z]+)$/) {
        if ($prefix eq 'get' || $prefix eq 'find') {
            if ($suffix =~ m/s$/) {
                my $singular = substr($suffix, 0, length($suffix) - 1);
                $singular =~ s/ie$/y/;
                $singular =~ s/^Metadata$/MetadataEntry/;
                $singular =~ s/^Policy$/ResourcePolicy/;
                return $singular.'[]';
            } else {
                my $singular = $suffix;
                $singular =~ s/ie$/y/;
                $singular =~ s/^Metadata$/MetadataEntry/;
                $singular =~ s/^Policy$/ResourcePolicy/;
                return $singular;
            }
        } elsif ($prefix eq 'add' || $prefix eq 'create') {
            my $response_representation = $xpc->find('wadl:response/wadl:representation[1]', $method)->[0];
            if (defined $response_representation) {
                my $element = $response_representation->getAttribute('element');
                if (defined $element && $element ne '') {
                    $element = transform_element($element);
                    return $element;
                } else {
                    print STDERR "response $id\n";
                    return 'Void';
                }
            }
        } elsif ($prefix eq 'update' || $prefix eq 'delete') {
            return 'Void';
        }
    }

    die $id;
}

sub is_optional_param {
    my $name = shift;
    if ($name eq 'userIP' || $name eq 'userAgent' || $name eq 'xforwardedfor') {
        return 1;
    } else {
        return 0;
    }
}

my $parser = XML::LibXML->new();

my $wadl = $parser->parse_file($wadl_file) or die;

sub traverse_resources {
    my ($node, $parent_path) = @_;
    $parent_path =~ s,/+$,,;
    my $resources = $xpc->find('wadl:resource', $node);
    foreach my $resource (@$resources) {
        my $path = $resource->getAttribute('path');
        $path =~ s,^/+,,;
        my $full_path = $parent_path.'/'.$path;
        my $path_params = $xpc->find('wadl:param', $resource);
        traverse_resources($resource, $parent_path.'/'.$path);
        traverse_methods($resource, $full_path, $path_params);
    }
}

sub traverse_methods {
    my ($resource, $full_path, $path_params) = @_;
    my $methods = $xpc->find('wadl:method', $resource);
    foreach my $method (@$methods) {
        my $return_type = get_return_type($method);

        my $method_name = $xpc->findvalue('@id', $method);

        my @overloads;
        push @overloads, {
            params_strs => [],
            params_code => '',
            url_code => "        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path(\"$full_path\")"
        };

        my $params = $xpc->find('wadl:request/wadl:param', $method);
        foreach my $param (@$path_params) {
            my $name = $param->getAttribute('name');
            foreach my $overl (@overloads) {
                push @{$overl->{params_strs}}, to_java_type($param->getAttribute('type')).' '.$name;
                $overl->{params_code} .= <<EOF
        if ($name != null) {
            uriVariables.put(\"$name\", $name);
        }
EOF
                ;
            }
        }
        foreach my $param (@$params) {
            my $name = $param->getAttribute('name');
            my @selection;
            if (is_optional_param($name)) {
                if (@overloads == 1) {
                    push @overloads, dclone($overloads[0]);
                }
                push @selection, $overloads[0];
            } else {
                @selection = @overloads;
            }
            foreach my $overl (@selection) {
                push @{$overl->{params_strs}}, to_java_type($param->getAttribute('type')).' '.$name;
                $overl->{url_code} .= "\n                .queryParam(\"$name\", $name)";
            }
        }
        foreach my $overl (@overloads) {
            $overl->{url_code} .= ';';
        }

        my $request_entity = 'toEntity(null)';
        my $request_representation = $xpc->find('wadl:request/wadl:representation[1]', $method)->[0];
        if (defined $request_representation) {
            my $element = $request_representation->getAttribute('element');
            my $request_media_type = $request_representation->getAttribute('mediaType');
            if (defined $element && $element ne '') {
                $element = transform_element($element);
                foreach my $overl (@overloads) {
                    push @{$overl->{params_strs}}, $element.' requestBody';
                }
                $request_entity = 'toEntity(requestBody)';
            } elsif ($request_media_type eq '*/*') {
                foreach my $overl (@overloads) {
                    push @{$overl->{params_strs}}, 'InputStream requestBody';
                }
                $request_entity = 'toEntity(new InputStreamResource(requestBody))';
            }
        }

        my $method_verb = $method->getAttribute('name');

        my $pre_call_code = '';
        my $post_call_code = '';
        if ($method_name eq 'login') {
            $post_call_code = <<EOF
        if (result == null || result.length() <= 0)
            result = null;
        dspaceToken = result;
EOF
            ;
        }

        foreach my $overl (@overloads) {
            my $params_str = join ', ', @{$overl->{params_strs}};
            my $build_code = 'build()';
            my $pre_call_code2 = '';
            if ($overl->{params_code} ne '') {
                $pre_call_code2 = "\n        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();\n".$overl->{params_code};
                $build_code = 'buildAndExpand(uriVariables)';
            }
            if ($method_name eq 'logout') {
                print <<EOF
    public $return_type $method_name($params_str) {
        try {
$pre_call_code$pre_call_code2$overl->{url_code}
            // "toUri" has a problem with encoding the query string, this is why we use "toUriString"
            $return_type result = restTemplate.exchange(queryBuilder.$build_code.toUriString(), HttpMethod.$method_verb,
                    $request_entity, $return_type.class).getBody();
$post_call_code
            return result;
        } finally {
            dspaceToken = null;
        }
    }

EOF
                ;
            } else {
                print <<EOF
    public $return_type $method_name($params_str) {
$pre_call_code$pre_call_code2$overl->{url_code}
        // "toUri" has a problem with encoding the query string, this is why we use "toUriString"
        $return_type result = restTemplate.exchange(queryBuilder.$build_code.toUriString(), HttpMethod.$method_verb,
                $request_entity, $return_type.class).getBody();
$post_call_code
        return result;
    }

EOF
                ;
            }
        }
    }
}

my $line;
while($line = <>) {
    if ($line =~ m/^\s*\[\% generated_code \%\]\s*$/) {
        traverse_resources($xpc->find('/wadl:application/wadl:resources', $wadl)->[0], '');
    } else {
        print $line;
    }
}

