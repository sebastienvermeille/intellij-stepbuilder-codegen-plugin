package com.github.sebastienvermeille.intellijstepbuildercodegenplugin.services

import com.github.sebastienvermeille.intellijstepbuildercodegenplugin.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
