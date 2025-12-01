# Git Workflow

## 1. Setup

### 1.1. Prerequisites

1. **Install Git:** Make sure you have Git installed. To check, run:
   ```bash
   git --version
   ```

### 1.2. Cloning the Repository

Before working on the code base for the first time, clone the repository:

1. Open your Terminal (e.g. Git Bash) or Command Prompt at the desired location or navigate to it using:
   ```bash
   cd /path/to/your/projects-directory
   ```
2. Clone the repository using one of the following command:
   ```bash
   git clone <repository-url>
   ```
   Where `<repository-url>` is `https://github.tik.uni-stuttgart.de/st197289/film-manager.git` for HTTPS or `git@github.tik.uni-stuttgart.de:st197289/film-manager.git` for ssh. The URL can also be copied from the green **Code** button on the repository page.
3. Navigate into the cloned repository:
   ```bash
   cd film-manager
   ```

# 2. Branching Model

The Branching Model is based on Git Flow.
We ensure that the `main` branch is always stable, while active development happens on the `develop` branch or dedicated feature/fix/... branches.

## 2.1. Core Branches

- `main` **(DO NOT PUSH HERE)**: this branch holds only stable code. New code is only added via pull requests.
- `develop`: This is where ongoing development happens. All new features or fixes are pushed or merged here.

## 2.2. Feature and Fix Branches

If a new feature, refactor fix etc. is relatively small, it can safely be done directly on the `develop` branch.

For larger features etc. or if multiple people are working on the same code, a new branch should be created from `develop`.
Use hyphens to separate words in branch names.

- **Feature Branches (new functionality)**:
  - Naming Convention: `feature/short-description`
  - Example: `feature/persistent-storage`
- **Fix Branches (bug fixes)**:
  - Naming Convention: `fix/short-description`
  - Example: `fix/login-bug`
- ...

Summary: Use new branches for larger changes or when collaborating with others. For minor changes (typo, few lines of code), working directly on `develop` is acceptable.

# 3. Starting Your Work

## 3.1. Switching to `develop`

Before writing any code, ensure you are on the `develop` branch and have the latest changes **(IMPORTANT)**:

```bash
git checkout develop
git pull origin develop
```
   
## 3.2. Creating a New Branch

Create a new branch with a descriptive name, using the `type/short-description` pattern:
```bash
git checkout -b feature/my-new-feature
```

# 4. Committing and Pushing

## 4.1. Stage, Commit, Push

1. Stage your changes: Run the following command for each file you modified (note that you can stage whole directories too):
   ```bash
   git add path/to/changed-file-or-directory
   # To stage all changes, use:
   git add .
   ```
   Try to group code changes logically when staging.
2. Commit your changes with a meaningful message:
   ```bash
   git commit -m "feature: add persistent storage for user settings"
   ```
   For a more in-depth explanation/guides on writing good commit messages, see section 9.2.
3. Push your branch to the remote repository:
   ```bash
   git push origin feature/my-new-feature
   ```
   
## 4.2. Setting the Upstream (first time only)

For the first time that you push a new branch, you might need to set the upstream branch:

```bash
git push --set-upstream origin feature/my-new-feature
# Alternatively, shorter:
git push -u origin feature/my-new-feature
```

For all subsequent pushes, you can then just use:

```bash
git push
```

# 5. Resolving Merge Conflicts on a single Branch

When two people change the same lines of a code on the same branch, a merge conflict might occur.
We resolve them by rebasing.
Rebasing is preferred over merging because it keeps the commit history linear and clean, making it easier to understand.

## Why Rebase?

Merging adds a new "Merge Commit," which can clutter history.
Rebasing takes your changes and moves them to the tip of the develop branch, making it look like you started your work after all recent changes were already made.

## 5.1. Rebasing Your Work

1. First, commit your changes as described in section 4.1.
2. Then, run the following command to rebase your commits onto the latest changes of the respective branch:
   ```bash
   git pull --rebase origin <branch>
   # E.g.
   git pull --rebase origin feature/my-new-feature
   # or if you are working directly on develop
   git pull --rebase origin develop
   ```
3. You might be lucky and have no conflicts. If so, just continue working as usual.

# 5.2. Manually Resolving Conflicts

If there are conflicts, Git will pause the rebase and notify you about the files with conflicts.

1. Open the conflicted files in your code editor.
2. You will see markers like these:
   ```
   <<<<<<< HEAD
   # Your changes
   =======
   # Remote changes
   >>>>>>> [commit hash or branch name]
   ```
3. Decide how to resolve the conflict by choosing one side, combining changes, or rewriting the section.
   Make sure to also remove the conflict markers.
4. Stage the resolved files:
   ```bash
   git add path/to/resolved-file
   ```
5. Continue the rebase process if there are more commits to be rebased:
   ```bash
    git rebase --continue
    ```
6. If there are more conflicts, repeat steps 1-5 until the rebase is complete.

**Note:** IntelliJ offers a user-friendly interface for resolving conflicts.
You will see corresponding buttons when a rebase is in progress, assisting you with steps 2-5.

# 6. Rebasing before Merging a Branch

Before merging your feature/fix/... branch into `develop`, it's good practice to rebase it onto the latest `develop` branch to ensure a clean merge.

Skip this step if you worked directly on `develop` or are unsure.

# 7. Submit your Work

When you feature/fix/etc. is complete and ready to be merged into `develop`, submit a Pull Request (PR) on GitHub.

## 7.1. Create a Pull Request

1. Go to the repository on GitHub and select the branch you want to merge into `develop`.
2. GitHub will usually show a prompt to create a Pull Request. Click on it. If not, go to the "Pull Requests" tab and click "New Pull Request."
3. Set the Base branch to `develop` (**IMPORTANT**) and the Compare branch to your feature/fix/... branch.
4. Fill out the title and description fields:
   - Provide a clear, descriptive title, e.g. "Implement persistent storage for user settings" or "Fix login bug causing crashes".
   - In the description, explain what changes were made, why they were made, and any other relevant details.

## 7.2. Review and Merge

1. Depending on how major the changes are, request reviews from team members.
   You can assign the reviewers on the right side of the PR page or just ask via Discord.
2. If changes are recommended during the review, make those changes in your local branch, commit, and push them.
   The PR will automatically update with the new commits.
3. Finally, merge the branch into `develop` or let someone else do it.

## 7.3. Clean Up

After merging into `develop`, make sure to pull the merged changes into your local `develop` branch:

```bash
git checkout develop
git pull origin develop # Or just git pull if upstream is set
```

# 8. Troubleshooting

This section will be covered in the future.

# 9. Notes

## 9.1. Pulling Latest Changes Before Starting New Work

If currently clean working directory (meaning no changes were made), it's sensible to always pull the latest changes the respective branch before starting new work:
```bash
git pull
```
Doing this helps to avoid merge conflicts later on.

## 9.2. Writing Good Commit Messages

A good commit message should be concise yet descriptive enough to understand the changes made.
You could use following pattern:

```xml
<type>(scope): <short-decsription>
```

### Type

- `feat`: New functionality added
- `fix`: Bug fix or typo correction
- `style`: Formatting, whitespace, missing semicolons, etc.
- `docs`: Documentation added or updated
- `chore`: Maintenance tasks, e.g. updating README, pom.xml, etc.
- `test`: Added modified test

### Scope (optional)

The scope indicates which part of the codebase the commit affects.
This is optional and could also be contained in the short description.

### Short Description

A brief summary of the changes made, written in the present tense.

### Additional Detail

If necessary, add a more detailed explanation of the changes in the body of the commit message.
To do so, use the `git commit` command without the `-m` flag, which will open your default text editor, then enter your commit message like so:

```xml
<type>(scope): <short-description>
↵
<detailed-explanation-of-the-changes>
```

### Examples

```
feat: add persistent storage for user settings
```

```
fix: correct typo in login error message
```

```
style: format code for better readability in UserService.java
```

```
docs: add documentation for media classes
```
