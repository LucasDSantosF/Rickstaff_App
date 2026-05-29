package com.rickstaff

import grails.gorm.annotation.Entity

@Entity
class Funcionario {

    String nome
    String email
    String cargo
    BigDecimal salario
    Boolean ativo = true
    Date dataCriacao = new Date()

    static constraints = {
        nome blank: false, maxSize: 100
        email blank: false, email: true, unique: true
        cargo blank: false, maxSize: 100
        salario min: 0.0
        ativo nullable: false
        dataCriacao nullable: false
    }

    static mapping = {
        dataCriacao column: 'data_criacao'
    }
}