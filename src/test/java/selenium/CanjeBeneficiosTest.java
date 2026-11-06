package selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanjeBeneficiosTest {

    private WebDriver driver;

    @AfterEach
    void cerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    // HU-023: verifica el descuento del pago y el saldo actualizado en la ficha.
    void canjeaPuntosRecalculaTotalYActualizaSaldo() throws IOException {
        String appUrl = System.getenv("APP_BASE_URL");
        String email = System.getenv("TEST_EMAIL");
        String password = System.getenv("TEST_PASSWORD");
        Assumptions.assumeTrue(
                appUrl != null && email != null && password != null,
                "Configura APP_BASE_URL, TEST_EMAIL y TEST_PASSWORD para ejecutar Selenium en QA");

        String driverPath = System.getenv("CHROMEDRIVER_PATH");
        if (driverPath != null && !driverPath.isBlank()) {
            System.setProperty("webdriver.chrome.driver", driverPath);
        }

        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get(appUrl);
        driver.findElement(By.cssSelector("input[type='email']")).sendKeys(email);
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement carritoMenu = wait.until(
                d -> d.findElement(By.cssSelector("[data-testid='menu-carrito']")));
        carritoMenu.click();
        wait.until(d -> d.findElements(By.cssSelector("[data-testid='producto-card']")).size() > 0);
        String configuredProduct = System.getenv("TEST_PRODUCT_NAME");
        WebElement productCard = driver.findElements(By.cssSelector("[data-testid='producto-card']"))
                .stream()
                .filter(card -> configuredProduct == null || card.getText().contains(configuredProduct))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No se encontró el producto de QA: " + configuredProduct));
        productCard.click();

        WebElement clienteSelect = wait.until(
                d -> d.findElement(By.cssSelector("[data-testid='cliente-fidelizacion']")));
        wait.until(d -> new Select(clienteSelect).getOptions().size() > 1);
        String configuredClient = System.getenv("TEST_CLIENT_NAME");
        WebElement clienteOption = new Select(clienteSelect).getOptions().stream()
                .filter(option -> !option.getAttribute("value").isBlank())
                .filter(option -> {
                    String balance = option.getAttribute("data-saldo-puntos");
                    String name = option.getText().split(" — ")[0];
                    return balance != null && Integer.parseInt(balance) >= 100
                            && (configuredClient == null || configuredClient.equals(name));
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No se encontró un cliente de QA activo con al menos 100 puntos"));
        String clienteId = clienteOption.getAttribute("value");
        String clienteNombre = clienteOption.getText().split(" — ")[0];
        int saldoInicial = Integer.parseInt(clienteOption.getAttribute("data-saldo-puntos"));
        new Select(clienteSelect).selectByValue(clienteId);
        guardarEvidencia("01-cliente-seleccionado");

        double totalAntes = Double.parseDouble(
                driver.findElement(By.cssSelector("[data-testid='total-venta']")).getText());
        driver.findElement(By.cssSelector("[data-testid='puntos-a-canjear']")).sendKeys("100");
        driver.findElement(By.cssSelector("[data-testid='aplicar-canje']")).click();
        wait.until(d -> d.findElements(By.cssSelector("[data-testid='canje-aplicado']")).size() == 1);
        double totalDespues = Double.parseDouble(
                driver.findElement(By.cssSelector("[data-testid='total-venta']")).getText());
        assertTrue(totalDespues < totalAntes, "El total debe disminuir después del canje");
        guardarEvidencia("02-descuento-y-total-recalculado");

        driver.findElement(By.cssSelector("input[placeholder='Ej. Uriel']"))
                .sendKeys("Evidencia HU-023 " + clienteNombre);
        new Select(driver.findElement(By.cssSelector("[data-testid='metodo-pago']")))
                .selectByValue("tarjeta");
        driver.findElement(By.cssSelector("[data-testid='cobrar-venta']")).click();
        WebElement saldoRestanteElement = wait.until(
                d -> d.findElement(By.cssSelector("[data-testid='saldo-puntos-restante']")));
        int saldoRestante = Integer.parseInt(saldoRestanteElement.getText().replaceAll("\\D+", ""));
        assertTrue(saldoRestante < saldoInicial, "El saldo debe disminuir después de cobrar");
        guardarEvidencia("03-venta-y-saldo-restante");

        WebElement menuClientes = wait.until(d -> d.findElement(By.xpath(
                "//nav[contains(@class,'menu')]//p[contains(normalize-space(.), 'Clientes')]")));
        menuClientes.click();
        WebElement buscarClientes = wait.until(d -> d.findElement(By.cssSelector(
                "[data-testid='buscar-clientes'], input[placeholder^='🔍 Buscar por nombre']")));
        buscarClientes.sendKeys(clienteNombre);
        WebElement fichaCliente = wait.until(d -> d.findElement(By.cssSelector(
                "[data-testid='ficha-cliente'][data-cliente-id='" + clienteId + "']")));
        List<WebElement> saldoEnFicha = fichaCliente.findElements(
                By.cssSelector("[data-testid='saldo-puntos-ficha']"));
        assertEquals(1, saldoEnFicha.size(), "La ficha debe mostrar el saldo actualizado");
        int saldoMostrado = Integer.parseInt(
                saldoEnFicha.get(0).getText().replaceAll("\\D+", ""));
        assertEquals(saldoRestante, saldoMostrado);
        guardarEvidencia("04-saldo-actualizado-en-ficha");
    }

    private void guardarEvidencia(String nombre) throws IOException {
        Path evidencia = Path.of(System.getenv().getOrDefault("EVIDENCE_DIR", "tests/evidence"));
        Files.createDirectories(evidencia);
        byte[] captura = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Files.write(evidencia.resolve(nombre + ".png"), captura);
    }
}
