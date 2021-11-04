require_relative 'ci/danger/assign_reviewers.rb'

warn("Big PR") if git.lines_of_code > 500

assign_reviewers(["jmartin", "marmatys", "dkadrikj", "nmarietta", "vbrison"])

cobertura.report = "build/reports/cobertura-coverage.xml"
cobertura.additional_headers = [:line, :branch]
cobertura.warn_if_file_less_than(percentage: 50)
cobertura.show_coverage
