#!/usr/bin/env nu

print "Deploying hardhat node... "
cd crypto-casino-contracts
npx hardhat node
sleep 5sec
cd ..

print "Deploying smart contracts..."
cd crypto-casino-contracts
npx hardhat ignition deploy ./ignition/modules/CasinoModule.ts --network localhost
cd ..

print "Starting Spring Boot backend..."
cd crypto-casino-backend
./gradlew.bat bootRun
cd ..

