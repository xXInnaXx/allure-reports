package ru.netology;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.*;

public class DeliveryCardTest {

    public String generateDate(int days) {
        return LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    @BeforeAll
    static void setUpAll() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide().screenshots(true).savePageSource(false));
    }

    @AfterAll
    static void tearDownAll() {
        SelenideLogger.removeAllListeners();
    }

    @BeforeEach
    void setUp() {
        open("http://localhost:9999/");
        $("[data-test-id='date'] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
    }


    @Test
    public void shouldSendForm() {
        String planningDate = generateDate(4);
        $("[placeholder='Город']").setValue("Казань");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("+79111234455");
        $("[data-test-id=date] input").setValue(planningDate);
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Успешно")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(visible)
                .shouldHave(exactText("Встреча успешно забронирована на " + planningDate));
    }

    @Test
    public void shouldFailBecausePhoneIsIncorrect() {
        String planningDate = generateDate(4);
        $("[placeholder='Город']").setValue("Казань");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("+6911123445");
        $("[data-test-id=date] input").setValue(planningDate);
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Успешно")).shouldBe(visible, Duration.ofSeconds(15));
    }

    @Test
    public void shouldNotSendFormWithIncorrectCity() {
        String planningDate = generateDate(3);
        $("[placeholder='Город']").setValue("ghggj");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("+79111234455");
        $("[data-test-id=date] input").setValue(planningDate);
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Доставка в выбранный город недоступна")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(hidden);

    }

    @Test
    public void shouldNotSendFormWithIncorrectDate() {
        String planningDate = generateDate(-3);
        $("[placeholder='Город']").setValue("Тверь");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("+79111234455");
        $("[data-test-id=date] input").doubleClick().sendKeys(Keys.DELETE);
        $("[data-test-id=date] input").setValue(planningDate);
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Заказ на выбранную дату невозможен")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(hidden);

    }

    @Test
    public void shouldNotSendNoRU() {
        String planningDate = generateDate(3);
        $("[placeholder='Город']").setValue("Тверь");
        $("[name='name']").setValue("Ivanov Ivan");
        $("[name='phone']").setValue("+79111234455");
        $("[data-test-id=date] input").setValue(planningDate);
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(hidden);

    }

    @Test
    public void shouldNotSendWithNumberPhoneMoreThan11() {
        String planningDate = generateDate(3);
        $("[placeholder='Город']").setValue("Тверь");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("+79111234455798789");
        $("[data-test-id=date] input").setValue(planningDate);
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(hidden);

    }

    @Test
    public void shouldNotSendWithNumberPhoneWithoutPlus() {
        String planningDate = generateDate(3);
        $("[placeholder='Город']").setValue("Тверь");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("79111234455");
        $("[data-test-id=date] input").setValue(planningDate);
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(hidden);

    }

    @Test
    public void shouldNotSendIfCheckboxNotSelected() {
        String planningDate = generateDate(3);
        $("[placeholder='Город']").setValue("Тверь");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("+79111234455");
        $("[data-test-id=date] input").setValue(planningDate);
        $("[data-test-id=agreement] input");
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Я соглашаюсь с условиями обработки и использования моих персональных данных")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(hidden);
    }
}