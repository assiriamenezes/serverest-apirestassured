/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package aulara;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;
import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import com.github.javafaker.Faker;

public class Exercicio2 {

    Faker faker = new Faker();
    String userName = faker.name().firstName();
    String email = userName + "@email.com";
    String productName = faker.commerce().productName();

    @BeforeClass
    public static void setup() {
        baseURI = "http://localhost";
        port = 3000;
    }

    public String CadastrarUsuario(String userName, String email) {

        //POST - CADASTRAR USUARIO
        String userID = given()
                .body("{\n" +
                        "  \"nome\": \"" + userName + "\",\n" +
                        "  \"email\": \"" + email + "\",\n" +
                        "  \"password\": \"teste\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/usuarios")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"))
                .extract().path("_id");

        return userID;
    }

    public String AutenticarUsuario() {

        //POST - AUTENTICAR USUARIO
        String token = given()
                .body("{\n" +
                        "  \"email\": \"" + email + "\",\n" +
                        "  \"password\": \"teste\"\n" +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Login realizado com sucesso"))
                .extract().path("authorization");

        return token;
    }

    public String CadastrarProduto(String token, String productName, int preco, String descricao, int qtd) {

        //POST - CADASTRAR PRODUTO
        String productId = given()
                .header("authorization", token)
                .body("{\n" +
                        "  \"nome\": \"" + productName + "\",\n" +
                        "  \"preco\": "+preco+",\n" +
                        "  \"descricao\": \""+descricao+"\",\n" +
                        "  \"quantidade\": "+qtd+"\n" +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/produtos")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"))
                .extract().path("_id");

        return productId;
    }

    public void CadastrarCarrinho(String token, String productId) {

        //POST - CADASTRAR CARRINHO
        given()
                .header("authorization", token)
                .body("{\n" +
                        "  \"produtos\": [\n" +
                        "    {\n" +
                        "      \"idProduto\": \"" + productId + "\",\n" +
                        "      \"quantidade\": 1\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/carrinhos")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"));

    }

    public void ExcluirUsuarioComCarrinho(String userID, String token) {

        // DELETE - EXCLUIR USUARIO COM CARRINHO
        given()
                .pathParam("_id", userID)
                .header("authorization", token)
                .when()
                .delete("usuarios/{_id}")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", is("N?o ? permitido excluir usu?rio com carrinho cadastrado"));
    }

    public void CancelarCompra(String token) {

        //DELETE - CANCELAR COMPRA
        given()
                .header("authorization", token)
                .when()
                .delete("/carrinhos/cancelar-compra")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro exclu?do com sucesso. Estoque dos produtos reabastecido"));

    }

    public void ExcluirProduto(String token, String productId) {

        //DELETE - EXCLUIR PRODUTO
        given()
                .pathParam("_id", productId)
                .header("authorization", token)
                .when()
                .delete("/produtos/{_id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro exclu?do com sucesso"));

    }

    public void ExcluirUsuario(String userID, String token){

        //DELETE - EXCLUIR USUARIO
        given()
                .pathParam("_id", userID)
                .header("authorization", token)
                .when()
                .delete("usuarios/{_id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro exclu?do com sucesso"));

    }

    @Test
    public void ValidarExclusaoUsuarioComCarrinho(){
        String userID = CadastrarUsuario(userName, email);
        String tokenUser = AutenticarUsuario();
        String productID = CadastrarProduto(tokenUser, productName, 50, "Panela", 40);
        CadastrarCarrinho(tokenUser, productID);
        ExcluirUsuarioComCarrinho(userID, tokenUser);
        CancelarCompra(tokenUser);
        ExcluirProduto(tokenUser, productID);
        ExcluirUsuario(userID, tokenUser);

    }
}
