#!/usr/bin/env bash
#
# Copyright (c) 2022 Proton Technologies AG
# This file is part of Proton AG and ProtonCore.
#
# ProtonCore is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# ProtonCore is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
#

set -eo pipefail

RELEASE_BRANCH=''

function prepare_git_credentials() {
  if [ -z "$GIT_CI_EMAIL" ] || [ -z "$GIT_CI_USERNAME" ] || [ -z "$PRIVATE_TOKEN_GITLAB_API_PROTON_CI" ]; then
    echo "Error, you must set a few variables (Settings -> CI/CD -> Variables) to be able tot commit"
    cat <<EOT
    - GIT_CI_EMAIL $GIT_CI_EMAIL
    - GIT_CI_USERNAME: $GIT_CI_USERNAME
    - PRIVATE_TOKEN_GITLAB_API_PROTON_CI: $PRIVATE_TOKEN_GITLAB_API_PROTON_CI
EOT
    exit 1
  fi

  git config --global user.email "$GIT_CI_EMAIL"
  git config --global user.name "$GIT_CI_USERNAME"
}

function update_remote_origin_url() {
  local user="https://${GIT_CI_USERNAME}:${PRIVATE_TOKEN_GITLAB_API_PROTON_CI}"

  # Ensure we convert git@xxx:xxxx/a.git to a URL friendly format
  local scope
  scope="$(awk -F '@' '{print $2}' <<<"$CI_REPOSITORY_URL" | tr ':' '/')"

  # Take https format and convert it to a SSH one so we can push from the CI
  local APP_GIT_CI="${user}@${scope}"

  # Gitlab default URL is https and the push doesn't work
  git remote set-url origin "$APP_GIT_CI"
}

function calculate_next_version() {
  if [ -z "$NEXT_VERSION" ]; then
    NEXT_VERSION=$(java -jar ./conventional-commits.jar next-version --repo-dir "${CI_PROJECT_DIR}")
  fi
}

function update_changelog() {
  echo "Generating changelog for ${NEXT_VERSION}"
  java -jar ./conventional-commits.jar changelog --next-version "${NEXT_VERSION}" --repo-dir "${CI_PROJECT_DIR}" --output "${CI_PROJECT_DIR}/CHANGELOG.md"
}

function prepare_new_release() {
  prepare_git_credentials
  update_remote_origin_url
  update_changelog

  git add CHANGELOG.md
  git commit -m "docs: Updated Changelog for version $NEXT_VERSION."

  git push origin "HEAD:$CI_COMMIT_REF_NAME" -o ci.skip

  #git checkout -b "$RELEASE_BRANCH"
  #git push origin "$RELEASE_BRANCH"
}

function main() {
  calculate_next_version
  RELEASE_BRANCH="release/libs/$NEXT_VERSION"

  # Make sure the release branch/tag doesn't exist yet
  if git show-ref "$RELEASE_BRANCH" --quiet; then
    echo "Branch or tag '$RELEASE_BRANCH' already exists, aborting."
    exit 1
  else
    prepare_new_release
  fi
}

main
