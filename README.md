# RandomMobOnBlock
Todo:

Save blocks and player in a file or database because:
1. OnReload blocks are resetet
2. OnReload alive player didnt reset when another died (needs to be checked if this is case and why)
3. On Server restart block are resetet
4. If a player dies after a server was restartet the player who didnt connect wont reset on the next login

Fixes:
OnReload event loads the blocks
OnEnable loads the blocks and if a player connects check if this players need to be resetet
