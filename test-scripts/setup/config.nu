let config = (
    open "../crypto-casino-backend/.env"
    | lines
    | where (not ($it | str trim | is-empty))
    | where (not ($it | str starts-with "#"))
    | parse "{key}={value}"
    | reduce --fold {} {|it, acc| $acc | insert $it.key $it.value }
)

export-env {
    $env.CASINO_TOKEN = $config.CONTRACT_CASINO_TOKEN
    $env.CASINO_VAULT = $config.CONTRACT_CASINO_VAULT
    $env.SLOT_MACHINE = $config.CONTRACT_SLOT_MACHINE
    $env.DICE = $config.CONTRACT_DICE
    $env.ROULETTE = $config.CONTRACT_ROULETTE
    $env.RPC_URL = $config.WEB3J_RPC_ADDRESS
    $env.BASE_URL = $"http://localhost:($config.SERVER_PORT)"
    $env.MASTER_WALLET_ADDRESS = $config.WALLET_MASTER_ADDRESS
    $env.PRIVATE_KEY = "0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d"
    $env.WALLET_ADDRESS = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8"
}

