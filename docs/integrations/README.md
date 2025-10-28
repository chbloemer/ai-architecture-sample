# Technology Integration Guides

Integration guides for specific technologies and frameworks.

## Available Guides

**[MCP Server Integration](./mcp-server-integration.md)**
MCP (Model Context Protocol) server exposing product catalog to AI assistants.

**Topics:** Architecture, available tools, client configuration, HTTP+SSE transport

**Use when:** Setting up AI assistant access, implementing custom MCP tools, debugging connectivity

---

**[Pug4j Template Engine Integration](./pug4j-integration.md)**
Pug template engine for server-side HTML rendering.

**Topics:** Setup, template syntax, layout inheritance, controller integration

**Use when:** Creating web pages with server-side rendering, working with templates

---

## Integration Philosophy

Integrations document how technologies connect to the core architecture. They are:
- **Replaceable** - Could swap technologies without affecting core architecture
- **Framework-independent core** - Domain layer remains independent
- **Adapter layer code** - Integration code lives in `portadapter.*`

## Related Documentation

- [Architecture Documentation](../architecture/) - Core patterns (DDD, Hexagonal, Onion)
- [Architecture Decision Records](../architecture/adr/) - Architectural decisions
- [Package Structure](../architecture/package-structure.md) - Package organization
