module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'header-max-length': [2, 'always', 72],
    'type-case': [2, 'always', 'lower-case'],
    'subject-max-length': [2, 'always', 50],
    'subject-full-stop': [2, 'never', '.'],
    'header-match-pattern': [
      2,
      'always',
      /^#\d+ (feat|fix|refac|test|chore|docs)(\([a-z0-9_-]+\))?: [^\s].+$/
    ]
  }
};

