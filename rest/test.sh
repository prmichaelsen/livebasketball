#!/bin/bash
cd lib
find -name "*.jar" -print | while read f; do
    echo "$f"
done
cd ..
