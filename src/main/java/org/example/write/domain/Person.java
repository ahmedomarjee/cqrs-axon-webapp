package org.example.write.domain;

import static java.lang.String.format;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.example.events.PersonRegistered;
import org.example.events.SexChanged;
import org.example.events.ThingBought;
import org.example.write.commands.BuyThing;
import org.example.write.commands.ChangeSex;
import org.example.write.commands.RegisterPerson;
import org.example.write.services.CatalogService;
import org.example.write.services.PaymentService;

public class Person extends AbstractAnnotatedAggregateRoot<String> {
    @AggregateIdentifier
    private String personId;
    private Gender gender;

    private Person() {
    }

    @EventHandler
    private void on(PersonRegistered personRegistered) {
        this.personId = personRegistered.getPersonId();
        this.gender = Gender.valueOfIgnoreCase(personRegistered.getGender());
    }

    @EventHandler
    private void on(SexChanged sexChanged) {
        this.gender = Gender.valueOfIgnoreCase(sexChanged.getGender());
    }

    @CommandHandler
    public Person(RegisterPerson registerPerson) {
        apply(new PersonRegistered(registerPerson.getPersonId(), registerPerson.getTitle(), registerPerson.getFirstName(),
                registerPerson.getLastName(), validBirthday(registerPerson.getBirthday()), validGender(registerPerson.getGender())));
    }

    @CommandHandler
    public void changeSex(ChangeSex changeSex) {
        if (!this.gender.canChangeTo(Gender.valueOfIgnoreCase(changeSex.getGender()))) {
            throw new IllegalArgumentException(format("Gender %s can not change to %s", this.gender, gender));
        }
        apply(new SexChanged(personId, changeSex.getGender()));
    }

    @CommandHandler
    public void buyThing(BuyThing buyThing, CatalogService catalogService, PaymentService paymentService) {
        double price = catalogService.priceFor(buyThing.getThing());
        paymentService.pay(price);
        apply(new ThingBought(personId, buyThing.getThing(), price));
    }

    private static String validGender(String gender) {
        Gender.valueOfIgnoreCase(gender);
        return gender;
    }

    private static String validBirthday(String birthday) {
        Birthday.valueOf(birthday);
        return birthday;
    }

}
