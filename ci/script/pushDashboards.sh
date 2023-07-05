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

function update_dashboards() {
  echo "Updating dashboards from ${GRAFANA_URL}..."
  ./gradlew :observability:observability-tools:run --args="download --grafana-api-key=${GRAFANA_API_KEY} --grafana-url=${GRAFANA_URL} --query=Android --output-dir=$CI_PROJECT_DIR/observability/dashboard"
}

function commitAndPushDashboards() {
  prepare_git_credentials
  update_remote_origin_url
  update_dashboards

  git add --all "$CI_PROJECT_DIR/observability/dashboard"
  git commit -m "docs: Updated Dashboards."

  git push origin "HEAD:$CI_COMMIT_REF_NAME" -o ci.skip
}

function main() {
  commitAndPushDashboards
}

main
