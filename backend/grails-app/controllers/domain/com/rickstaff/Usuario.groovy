package com.rickstaff

import grails.gorm.annotation.Entity

@Entity
class Usuario {
    String nome
    String email
    String senha

    static constraints = {
        nome blank: false
        email email: true, unique: true, blank: false
        senha blank: false
    }

    static mapping = {
        table 'usuario'
    }
}