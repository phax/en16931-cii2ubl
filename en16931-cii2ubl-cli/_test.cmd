@REM
@REM Copyright (C) 2019-2021 Philip Helger
@REM http://www.helger.com
@REM philip[at]helger[dot]com
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM         http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off
java -jar target\en16931-cii2ubl-cli%V%-full.jar  src\test\resources\CII_example1.xml
java -jar target\en16931-cii2ubl-cli%V%-full.jar  src\test\resources\not-existing.xml

