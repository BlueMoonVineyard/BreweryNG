#!/usr/bin/env bash

shopt -s globstar

xgettext --from-code=UTF-8 -Ljava src/**/*.java -k -ktr:1 -ktrc:1c,2 -ktrn:1,2  -o resources/i18n/messages.pot
