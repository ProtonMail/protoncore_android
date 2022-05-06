require './ci/danger/conventional_commits.rb'

warn("Big PR") if git.insertions > 1000

random_reviewers.assign(["jmartin", "marmatys", "dkadrikj", "nmarietta"])

cobertura.report = "build/reports/cobertura-coverage.xml"
cobertura.additional_headers = [:line, :branch]
cobertura.warn_if_file_less_than(percentage: 50)
cobertura.show_coverage

commit_message_errors = check_conventional_commits()
if !commit_message_errors.empty?
  formatted_errors = commit_message_errors.join("\n-")
  warn("All commit messages must conform to [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) spec.\n- #{formatted_errors}")
end
