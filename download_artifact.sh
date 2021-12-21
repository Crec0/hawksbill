#! /bin/sh

readonly owner=$1
if [ -z "$owner" ]; then
  printf "Usage: $0 <owner> <repo>\n"
  exit 1
fi

readonly repo=$2
if [ -z "$repo" ]; then
  printf "Usage: $0 <owner> <repo>\n"
  exit 1
fi

# python command that parses the curl output and gives the latest successful run's artifact URL
readonly parseArtifactUrlPython="import json;import sys;sys.stdout.write(str(next(map(lambda obj: obj['artifacts_url'], filter(lambda obj: obj['conclusion'] == 'success', json.load(sys.stdin)['workflow_runs'])), None)))"

# the github api runs urs
readonly runsUrl="https://api.github.com/repos/$owner/$repo/actions/runs"

# header for the github api json response
readonly accept="Accept: application/vnd.github.v3+json"

# requst, parse and store the latest id in the variable
# no reruns required since this will be used for one specific repo
readonly artifactUrl=$(curl --netrc-file _netrc -s -H $accept -H "per_page:50" -H "page:0" $runsUrl | python3 -c "$parseArtifactUrlPython")

if [ -z "$artifactUrl" ]; then
  printf "No successful runs found for $owner/$repo\n"
  exit 1
fi

printf "Aquired Artifact URL\n"

# python command to parse the artifact url and extract archive url
readonly parseArchiveUrlPython="import json;import sys;sys.stdout.write(str(next(map(lambda obj: obj['archive_download_url'], json.load(sys.stdin)['artifacts']), None)))"

# request, parse, store latest artifact archive url
readonly archiveDownladUrl=$(curl --netrc-file _netrc -s -H $accept $artifactUrl | python3 -c "$parseArchiveUrlPython")

if [ -z "$archiveDownladUrl" ]; then
  printf "No successful run found\n"
  exit 1
fi

printf "Aquired Archive Download URL\n"

# delete the old zip file if it exists
if test -f "$repo.zip"; then
    rm $repo.zip
    printf "Deleted old zip file\n"
fi

# download the latest artifact archive
curl --netrc-file _netrc -s -L -o "$repo.zip" $archiveDownladUrl

printf "Downloaded latest artifact archive\n"

# delete any jar if present
if [ `ls *.jar 2> /dev/null | wc -l` -gt 0 ]; then
    rm *.jar
    printf "Deleted old jar files\n"
fi

# extract the zip file
unzip $repo.zip 1> /dev/null

printf "Extracted zip file\n"

# deleting *-dev.jar and *-sources.jar
if [ `ls *-dev.jar *-sources.jar 2> /dev/null | wc -l` -gt 0 ]; then
    rm *-dev.jar *-sources.jar
    printf "Deleted dev and sources files\n"
fi

# delete the zip file
if test -f "$repo.zip"; then
    rm $repo.zip
    printf "Deleted zip file\n"
fi

# rename the jar file to the easier name
mv *.jar bot.jar