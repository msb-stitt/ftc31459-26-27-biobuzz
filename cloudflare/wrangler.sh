#!/usr/bin/env bash
# Status: controlling. Runs the pinned wrangler as the account coding.stitt.cc is on. The API
# token is CLOUDFLARE_API_TOKEN, or on a Mac the login Keychain's, service cloudflare-api, account
# coding-stitt-cc-mac. wrangler's settings, logs and caches go to .state/ here, not to ~/.
D=$(cd "$(dirname "$0")" && pwd)
if [ -z "$CLOUDFLARE_API_TOKEN" ]; then
  CLOUDFLARE_API_TOKEN=$(security find-generic-password -s cloudflare-api -a coding-stitt-cc-mac -w) ||
    { echo "no CLOUDFLARE_API_TOKEN, and no cloudflare-api token in the Keychain" >&2; exit 1; }
  export CLOUDFLARE_API_TOKEN
fi
export CLOUDFLARE_ACCOUNT_ID=8750f54dd1cc6ffc4a7d066b02d08d85
export XDG_CONFIG_HOME=$D/.state XDG_CACHE_HOME=$D/.state WRANGLER_SEND_METRICS=false
exec "$D/node_modules/.bin/wrangler" "$@"
