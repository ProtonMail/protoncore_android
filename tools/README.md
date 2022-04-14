# Proton Core Tools

### conventional-commits

The tool can be used to verify commit messages, propose new version, generate and update the changelog.

#### Usage in CI

By launching the `deploy-build-tools` job on CI, the tool will be built and uploaded into package registry.

To use the tool in a CI job, you need to download it from package registry.
To do that, reference the `.download-tools` script in your job.
