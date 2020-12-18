#!/usr/bin/env groovy
def jenkinsfile

def overrides = [
    scriptVersion              : 'v7',
    iqOrganizationName         : "Team AOS",
    pipelineScript             : 'https://git.aurora.skead.no/scm/ao/aurora-pipeline-scripts.git',
    checkstyle                 : false,
    docs                       : false,
    javaVersion                : 11,
    jiraFiksetIKomponentversjon: true,
    chatRoom                   : "#aos-notifications",
    versionStrategy            : [
        [branch: 'master', versionHint: '0']
    ]
]

fileLoader.withGit(overrides.pipelineScript, overrides.scriptVersion) {
  jenkinsfile = fileLoader.load('templates/leveransepakke')
}
jenkinsfile.gradle(overrides.scriptVersion, overrides)