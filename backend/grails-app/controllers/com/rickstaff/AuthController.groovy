package com.rickstaff

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import java.util.UUID

@Transactional
class AuthController {

    static responseFormats = ['json']

    def login() {
        def body = request.JSON

        if (body == null) {
            response.status = 400
            render([success: false, message: "Corpo da requisição vazio ou formato inválido"] as JSON)
            return
        }

        if (!body?.email || !body?.senha) {
            response.status = 400
            render([success: false, message: "Email e senha são obrigatórios"] as JSON)
            return
        }

        def usuario = Usuario.executeQuery(
            "FROM Usuario WHERE email = :email", 
            [email: body.email as String]
        ).find()

        if (usuario == null) {
            response.status = 401
            render([success: false, message: "Usuário não encontrado"] as JSON)
            return
        }

        if (usuario.senha == body.senha) {
            render([
                success: true,
                token: UUID.randomUUID().toString(),
                usuario: [
                    id: usuario.id,
                    nome: usuario.nome,
                    email: usuario.email
                ]
            ] as JSON)
        } else {
            response.status = 401
            render([success: false, message: "Credenciais inválidas"] as JSON)
        }
    }

    def register() {
        def body = request.JSON

        if (!body?.email || !body?.senha || !body?.nome) {
            response.status = 400
            render([success: false, message: "Nome, email e senha são obrigatórios"] as JSON)
            return
        }

        def usuario = Usuario.executeQuery(
            "FROM Usuario WHERE email = :email", 
            [email: body.email as String]
        ).find()

        if (usuario != null) {
            response.status = 400
            render([success: false, message: "Usuário já existe"] as JSON)
            return
        }

        def novo_usuario = new Usuario(nome: body.nome, email: body.email, senha: body.senha)

        if (novo_usuario.save(flush: true)) {
            render([
                success: true,
                token: UUID.randomUUID().toString(),
                usuario: [
                    id: novo_usuario.id,
                    nome: novo_usuario.nome,
                    email: novo_usuario.email
                ]
            ] as JSON)
        } else {
            response.status = 400
            render([success: false, message: "Erro ao criar usuário: ${novo_usuario.errors.allErrors}"] as JSON)
        }
    }
}