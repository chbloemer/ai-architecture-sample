# Technology Integration Guides

This directory contains guides for integrating specific technologies and frameworks with the AI Architecture Sample Project.

## Available Guides

### [MCP Server Integration](./mcp-server-integration.md)
**Model Context Protocol (MCP) Server Integration**

Learn how to integrate and use the MCP server that exposes the product catalog to AI assistants like Claude.

**Topics covered:**
- MCP server architecture and design
- Available MCP tools (all-products, product-by-sku, product-by-category, product-by-id)
- Connecting AI assistants via `.mcp.json` configuration
- HTTP + SSE transport protocol
- Tool validation and error handling

**Use this when:**
- Setting up AI assistant access to the product catalog
- Understanding how MCP fits into the hexagonal architecture
- Implementing custom MCP tools
- Debugging MCP server connectivity

---

### [Pug4j Template Engine Integration](./pug4j-integration.md)
**Pug4j Template Engine for Server-Side Rendering**

Guide to using the Pug template engine for rendering HTML views in the web MVC layer.

**Topics covered:**
- Pug4j setup and configuration with Spring Boot
- Template syntax and features
- Layout inheritance and includes
- Passing data from controllers to views
- Template location and organization

**Use this when:**
- Creating new web pages with server-side rendering
- Understanding the View layer in the web MVC pattern
- Customizing the HTML rendering pipeline
- Working with template layouts and partials

---

## Integration Philosophy

These integration guides document **how specific technologies connect to the core architecture** without being part of the architecture itself.

**Key principles:**
- Integrations are replaceable (could swap Pug4j for Thymeleaf, MCP for GraphQL, etc.)
- Core architecture remains framework-independent
- Integration code lives in adapter layers (`portadapter.*`)
- Documentation focuses on "how to use" rather than "why we architected this way"

---

## Related Documentation

For architectural decisions and patterns, see:
- **[Architecture Documentation](../architecture/)** - Core architectural patterns (DDD, Hexagonal, Onion)
- **[Architecture Decision Records](../architecture/adr/)** - Documented architectural decisions
- **[Package Structure](../architecture/package-structure.md)** - How packages are organized

---

## Contributing Integration Guides

When adding a new integration guide:

1. **File naming**: Use kebab-case (e.g., `my-technology-integration.md`)
2. **Update this README**: Add your guide to the list above with description
3. **Include examples**: Show practical code examples
4. **Link to architecture**: Explain where the integration fits in the architecture
5. **Configuration**: Document all required setup and configuration

**Template structure:**
```markdown
# Technology Name Integration

## Overview
Brief description of what this technology does and why we use it.

## Architecture Integration
Explain where this fits in the hexagonal/onion architecture.

## Setup
Step-by-step setup instructions.

## Usage
How to use this technology in the project.

## Configuration
Required configuration files and properties.

## Examples
Practical code examples.

## Troubleshooting
Common issues and solutions.
```

---

**Last Updated**: October 27, 2025