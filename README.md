# AntiSpam
A anti-spam plugin for bukkit based minecraft server.

## Generic triggers

Plugins can trigger named event then AntiSpam process and output result.

```groovy
anti_spam_valid = [
    player: Player,
    msg: String
]

[
    result: ["pass", "spam", "blacklist"]
]
```
