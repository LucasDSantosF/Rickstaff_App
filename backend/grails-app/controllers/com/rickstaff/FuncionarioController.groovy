package com.rickstaff

import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class FuncionarioController {

    static responseFormats = ['json']
    static allowedMethods = [
        index: 'GET',
        show: 'GET',
        save: 'POST',
        update: 'PUT',
        delete: 'DELETE'
    ]

    def index() {
        def funcionarios = Funcionario.executeQuery("FROM Funcionario ORDER BY nome")
        render funcionarios as JSON
    }

    def show(Long id) {
        def f = Funcionario.executeQuery("FROM Funcionario WHERE id = :id", [id: id]).find()
        if (!f) {
            response.status = 404
            render([message: "Funcionário não encontrado"] as JSON)
            return
        }
        render f as JSON
    }

    def save() {
        def body = request.JSON
        def f = new Funcionario(
            nome: body.nome,
            email: body.email,
            cargo: body.cargo,
            salario: body.salario as BigDecimal,
            ativo: body.ativo != null ? body.ativo : true
        )

        if (!f.save(flush: true)) {
            response.status = 422
            render([errors: f.errors.allErrors.collect { it.defaultMessage }] as JSON)
            return
        }
        response.status = 201
        render f as JSON
    }

    def update(Long id) {
        def f = Funcionario.executeQuery("FROM Funcionario WHERE id = :id", [id: id]).find()
        if (!f) {
            response.status = 404
            render([message: "Funcionário não encontrado"] as JSON)
            return
        }

        def body = request.JSON
        f.nome = body.nome ?: f.nome
        f.email = body.email ?: f.email
        f.cargo = body.cargo ?: f.cargo
        if (body.salario != null) f.salario = body.salario as BigDecimal
        if (body.ativo != null) f.ativo = body.ativo

        if (!f.save(flush: true)) {
            response.status = 422
            render([errors: f.errors.allErrors.collect { it.defaultMessage }] as JSON)
            return
        }
        render f as JSON
    }

    def delete(Long id) {
        def f = Funcionario.executeQuery("FROM Funcionario WHERE id = :id", [id: id]).find()
        if (!f) {
            response.status = 404
            render([message: "Funcionário não encontrado"] as JSON)
            return
        }
        f.delete(flush: true)
        response.status = 204
        render ""
    }
}