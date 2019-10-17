pipelineWithMavenAndDocker {
    enableDependencyTrack = true
    verificationEnvironment = 'eid-verification2'
    stagingEnvironment = 'eid-staging'
    stagingEnvironmentType = 'puppet2'
    productionEnvironment = 'eid-production'
    gitSshKey = 'ssh.github.com'
    puppetModules = 'idporten_bankidmobil'
    puppetApplyList = ['eid-systest-app01.dmz.local baseconfig,idporten_bankidmobil']
}
