#!/usr/bin/env bash
source ~/.bashrc

set -ex

npm ci

export NODE_ENV=production;

npx gulp dist:clean || exit 1;
npx gulp --theme=${UXBOX_THEME} dist || exit 1;

clojure -Adev tools.clj dist:all || exit 1

npx gulp dist:gzip || exit 1

