#!/usr/bin/env bash
#
# Copyright (c) 2021 Proton Technologies AG
# This file is part of Proton Technologies AG and ProtonCore.
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

# branch release/X.Y.Z produces X.Y.Z version
# other branch like feat/A produces feat-A-SNAPSHOT version

set -eo pipefail

if [[ -n "$CI_COMMIT_REF_NAME" ]]
then
  # On the CI, using predefined var
  branch_name=$CI_COMMIT_REF_NAME
else
  # Fallback on local git state
  branch_name=$(git rev-parse --abbrev-ref HEAD)
fi

if [[ "$branch_name" =~ ^release\/.* ]]
then
  # Extract release version
  version=${branch_name#release\/}
else
  # Append snapshot and replace / by - for maven version compatibility
  version=${branch_name/\//-}-SNAPSHOT
fi
echo "$version"
