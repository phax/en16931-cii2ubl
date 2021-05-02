@echo off
java -jar target\en16931-cii2ubl-cli%V%-full.jar  src\test\resources\CII_example1.xml
java -jar target\en16931-cii2ubl-cli%V%-full.jar  src\test\resources\not-existing.xml

