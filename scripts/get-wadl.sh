#!/bin/sh

set -e
set -x

OUTPUT_DIR='src/main/resources/org/dspace/demo'
DEMO_URL='https://demo.dspace.org'

for f in application.wadl application.wadl/xsd0.xsd
do
    g=$(echo "$f" | sed 's,/,_/,g')
    dn=$(dirname "${OUTPUT_DIR}/rest/${g}")
    mkdir -p "$dn"
    curl -k "${DEMO_URL}/rest/${f}" >| "${OUTPUT_DIR}/rest/${g}"
    if [ "$f" != "application.wadl" ]
    then
        perl -pi -e 's,application.wadl/,application.wadl_/,g' "${OUTPUT_DIR}/rest/application.wadl"
    fi
done

