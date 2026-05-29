package rickstaff.backend

import com.rickstaff.Usuario
import grails.gorm.transactions.Transactional

class BootStrap {

    def init = { servletContext ->
        criarAdminSeNecessario()
    }

    @Transactional
    void criarAdminSeNecessario() {
        if (!Usuario.executeQuery("FROM Usuario WHERE email = 'admin@empresa.com'").find()) {
            new Usuario(
                nome: "Administrador",
                email: "admin@empresa.com",
                senha: "123456"
            ).save(flush: true)
            println ">>> Usuário admin criado com sucesso!"
        } else {
            println ">>> Usuário admin já existe, pulando criação."
        }
    }

    def destroy = {}
}
