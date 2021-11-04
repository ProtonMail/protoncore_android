warn("Big PR") if git.insertions > 1000

random_reviewers.assign(["jmartin", "marmatys", "dkadrikj", "nmarietta", "vbrison"])

cobertura.report = "build/reports/cobertura-coverage.xml"
cobertura.additional_headers = [:line, :branch]
cobertura.warn_if_file_less_than(percentage: 50)
cobertura.show_coverage
