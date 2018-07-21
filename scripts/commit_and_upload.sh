#!/bin/bash

RED='\033[0;31m'
NC='\033[0m' # No Color

POSITIONAL=()
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -b|--activation-branch)
    BRANCH="$2"
    shift # past argument
    shift # past value
    ;;
    -s|--sourcekey)
    SOURCE="$2"
    shift # past argument
    shift # past value
    ;;
    -d|--destination)
    DESTINATION="$2"
    shift # past argument
    shift # past value
    ;;
    -r|--repository)
    REPOSITORY="$2"
    shift # past argument
    shift # past value
    ;;
    *)    # unknown option
    POSITIONAL+=("$1") # save it in an array for later
    shift # past argument
    ;;
esac
done
set -- "${POSITIONAL[@]}" # restore positional parameters

if [ -z "${BRANCH}" ]; then printf "${RED}Travis activation branch unspecified.${NC}\n"; fi
if [ -z "${SOURCE}" ]; then printf "${RED}Source folder filter unspecified.${NC}\n"; fi
if [ -z "${DESTINATION}" ]; then printf "${RED}Destination folder unspecified.${NC}\n"; fi
if [ -z "${REPOSITORY}" ]; then printf "${RED}Repository unspecified.${NC}\n"; fi

if [ ! -z "${BRANCH}" ] &&
 [ ! -z "${SOURCE}" ] &&
 [ ! -z "${DESTINATION}" ] &&
 [ ! -z "${REPOSITORY}" ]; then

    if [ -n "${GITHUB_API_KEY}" ]
      [ "${TRAVIS_PULL_REQUEST}" == "false" ] &&
      [ "${TRAVIS_BRANCH}" == "${BRANCH}" ]; then

        echo -e "Publishing ${DESTINATION} into ${REPOSITORY} (${BRANCH}) from all ${SOURCE}:\n"
        mkdir $HOME/git_upload_tmp
        find -path "*/${SOURCE}" -print0 | while IFS= read -r -d $'\0' line; do ls $line; cp -r $line/* $HOME/git_upload_tmp; done;
        cd $HOME
        git config --global user.email "travis@travis-ci.org"
        git config --global user.name "travis-ci"
        git clone --quiet --branch=gh-pages https://iugin:${GITHUB_API_KEY}@github.com/${REPOSITORY} gh-pages > /dev/null
        cd gh-pages
        git rm -rf ./${DESTINATION}
        cp -Rf $HOME/git_upload_tmp ./${DESTINATION}
        git add -f .
        git commit -m "Latest ${DESTINATION} on successful travis build ${TRAVIS_BUILD_NUMBER} auto-pushed to gh-pages"
        git push -fq origin gh-pages > /dev/null
        echo -e "Published ${DESTINATION} to gh-pages.\n"
    fi
fi



