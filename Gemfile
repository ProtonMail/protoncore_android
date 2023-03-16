# frozen_string_literal: true

source "https://rubygems.org"

git_source(:github) { |repo_name| "https://github.com/#{repo_name}" }

# gem "rails"

gem "danger-gitlab", "~> 8.0"
gem "danger-cobertura", :git => ENV['DANGER_COBERTURA_GIT_URL']
gem "danger-random_reviewers", :git => ENV['DANGER_RANDOM_REVIEWERS_GIT_URL'], branch: 'main'
gem "git", "~> 1.17.2", require: false
