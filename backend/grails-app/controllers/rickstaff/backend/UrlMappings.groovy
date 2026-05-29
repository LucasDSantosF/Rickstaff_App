package rickstaff.backend

class UrlMappings {
    static mappings = {
        "/api/auth/login"(controller: "auth", action: "login", method: "POST")
        "/api/auth/register"(controller: "auth", action: "register", method: "POST")


        "/api/funcionarios"(controller: "funcionario") {
            action = [GET: "index", POST: "save"]
        }
        "/api/funcionarios/$id"(controller: "funcionario") {
            action = [GET: "show", PUT: "update", DELETE: "delete"]
        }

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}