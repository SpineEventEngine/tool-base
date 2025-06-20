# Guidance for Contributors and Agents

This project follows best practices recommended by leading LLM vendors (OpenAI, Anthropic, Google, etc.). Key points include:

- Write clear, incremental commits with descriptive messages.
- Include automated tests for any code change that alters functionality.
- Keep pull requests focused and small.
- Adhere to the [Spine Event Engine Documentation](https://github.com/SpineEventEngine/documentation/wiki) for coding style and contribution procedures.
- When modifying code, run `./gradlew build` before committing. Documentation-only changes do not require running tests.

