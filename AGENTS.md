# Instructions for AI Agents

## Eclipse Contributor Requirements

This project follows the [Eclipse Contributor Agreement](CONTRIBUTING.md) requirements.
All contributors — including AI agents acting on behalf of a user — **must** comply with the
[Eclipse Project Handbook](https://www.eclipse.org/projects/handbook/#resources-commit),
which requires every commit to identify the contributor by their legal name.

## Also-by Requirement

When an AI agent creates or co-authors a commit on behalf of a user, the commit message **must**
include an `Also-by:` line with the user's **legal name** (as shown on their GitHub profile, not
their GitHub handle) and their **real email address** (not GitHub's `noreply` placeholder address).

Format:

```
Also-by: Full Legal Name <real-email@example.com>
```

### How to obtain the user's information

Find the user's **name** and **email** from their recent commits in the repository.
Look for commits authored directly by the user (not by bots).

- If GitHub MCP tools are available, use `github-mcp-server-list_commits` /
  `github-mcp-server-get_commit` — the `commit.author.name` and `commit.author.email`
  fields contain the required values.
- Otherwise, use `git log --format='%aN <%aE>'` locally to extract the same information.

If no prior commits exist, prompt the user to provide their legal name and email.

### Example commit message

```
Fix null pointer exception in ECL editor

Also-by: Jane Doe <jane.doe@example.com>
```
