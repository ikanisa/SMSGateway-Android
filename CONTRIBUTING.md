# Contributing to SMS Gateway Android

Thank you for considering contributing to SMS Gateway!

## Development Setup

1. Fork the repository
2. Clone your fork
3. Create a feature branch: `git checkout -b feature/my-feature`
4. Make your changes
5. Run tests: `./gradlew testDebugUnitTest`
6. Commit: `git commit -m "feat: add my feature"`
7. Push: `git push origin feature/my-feature`
8. Open a Pull Request

## Code Style

- **Kotlin**: Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Compose**: Use Material 3 components
- **Naming**: Use camelCase for functions, PascalCase for classes
- **Comments**: Add KDoc for public APIs

## Commit Messages

Format: `<type>: <description>`

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance

## Testing

- Add unit tests for new features
- Maintain 80%+ code coverage
- Run `./gradlew testDebugUnitTest` before submitting PR

## Pull Request Guidelines

- [ ] Tests pass
- [ ] Code follows style guidelines
- [ ] Commit messages follow convention
- [ ] PR description explains changes
