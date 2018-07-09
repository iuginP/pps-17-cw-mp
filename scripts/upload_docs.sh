#!/bin/bash

if [ -n "$GITHUB_API_KEY" ]
  [ "$TRAVIS_PULL_REQUEST" == "false" ] &&
  [ "$TRAVIS_BRANCH" == "master" ]; then

  echo -e "Publishing docs:\n"
  mkdir $HOME/latest-javadoc
  mkdir $HOME/latest-scaladoc
  find -path "*/latest-docs/java" -print0 | while IFS= read -r -d $'\0' line; do ls $line; cp -r $line/* $HOME/latest-javadoc; done;
	find -path "*/latest-docs/scala" -print0 | while IFS= read -r -d $'\0' line; do ls $line; cp -r $line/* $HOME/latest-scaladoc; done;
	cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet --branch=gh-pages https://iugin:$GITHUB_API_KEY@github.com/iuginP/pps-17-cw-mp-tests gh-pages > /dev/null
  cd gh-pages
  git rm -rf ./java
  git rm -rf ./scala
  cp -Rf $HOME/latest-javadoc ./java
  cp -Rf $HOME/latest-scaladoc ./scala
  git add -f .
  git commit -m "Latest docs on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null
  echo -e "Published docs to gh-pages.\n"
fi
