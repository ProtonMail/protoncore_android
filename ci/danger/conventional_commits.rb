def check_conventional_commits()
  target_branch = ENV["CI_MERGE_REQUEST_TARGET_BRANCH_NAME"]
  current_commit_sha = ENV["CI_COMMIT_SHA"]
  current_branch = ENV["CI_MERGE_REQUEST_SOURCE_BRANCH_NAME"]
  repo_dir = ENV["CI_PROJECT_DIR"]

  puts("Fetching #{current_branch} shallow-exclude=#{target_branch}")
  system("git fetch origin #{current_branch} --shallow-exclude=#{target_branch} && git fetch --deepen=1 || echo 'Cannot fetch shallow - fetching with unshallow' && git fetch origin #{current_branch} --unshallow")

  puts("Getting first commit hash #{target_branch}..#{current_commit_sha}")
  first_commit_for_mr = `git -C "#{repo_dir}" log "refs/remotes/origin/#{target_branch}..#{current_commit_sha}" --pretty=format:'%H' | tail -1`

  puts "Checking conventional commits from commit #{first_commit_for_mr}"
  system("java", "-jar", "./conventional-commits.jar", "verify-commit", "--verbose", "--from-commit-sha", first_commit_for_mr, "--repo-dir", repo_dir)
end
