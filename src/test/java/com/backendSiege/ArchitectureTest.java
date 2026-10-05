package com.backendSiege;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Garde-fous de l'architecture hexagonale :
 * <pre>
 *   api  ──▶  domain  ◀──  infrastructure
 * </pre>
 * Le domaine (modèles, ports, services) ne dépend de rien d'extérieur ; l'API REST et l'infrastructure
 * (clients des backends pays, persistance, sécurité) dépendent du domaine, jamais l'inverse ni l'un de l'autre.
 */
class ArchitectureTest {

    private static final String RACINE = "com.backendSiege";
    private static final String DOMAIN = RACINE + ".domain..";
    private static final String API = RACINE + ".api..";
    private static final String INFRASTRUCTURE = RACINE + ".infrastructure..";

    private static JavaClasses classesDeProduction;

    @BeforeAll
    static void importerLesClasses() {
        classesDeProduction = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(RACINE);
    }

    private static void verifier(ArchRule regle) {
        regle.check(classesDeProduction);
    }

    @Test
    void les_classes_de_production_sont_bien_importees() {
        org.assertj.core.api.Assertions.assertThat(classesDeProduction.size()).isGreaterThan(100);
    }

    @Test
    void les_couches_respectent_le_sens_des_dependances() {
        verifier(layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Domaine").definedBy(DOMAIN)
                .layer("API").definedBy(API)
                .layer("Infrastructure").definedBy(INFRASTRUCTURE)
                .whereLayer("API").mayNotBeAccessedByAnyLayer()
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .whereLayer("Domaine").mayOnlyBeAccessedByLayers("API", "Infrastructure"));
    }

    @Test
    void le_domaine_ne_depend_ni_de_l_api_ni_de_l_infrastructure() {
        verifier(noClasses().that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat().resideInAnyPackage(API, INFRASTRUCTURE));
    }

    @Test
    void le_domaine_est_independant_des_frameworks() {
        verifier(noClasses().that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta..", "javax..", "org.hibernate..", "com.fasterxml.jackson..",
                        "tools.jackson..", "org.mapstruct..", "reactor..", "io.jsonwebtoken..", "org.slf4j..", "lombok..")
                .because("le domaine ne doit connaître ni le web, ni la persistance, ni la sérialisation, ni le transport HTTP"));
    }

    @Test
    void l_api_ne_depend_pas_de_l_infrastructure() {
        verifier(noClasses().that().resideInAPackage(API).should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE));
    }

    @Test
    void l_infrastructure_ne_depend_pas_de_l_api() {
        verifier(noClasses().that().resideInAPackage(INFRASTRUCTURE).should().dependOnClassesThat().resideInAPackage(API));
    }

    @Test
    void les_couches_ne_forment_pas_de_cycle() {
        verifier(slices().matching(RACINE + ".(*)..").should().beFreeOfCycles());
    }

    @Test
    void les_controleurs_ne_connaissent_que_les_services_du_domaine() {
        verifier(noClasses().that().resideInAPackage(RACINE + ".api.controller..")
                .should().dependOnClassesThat().resideInAnyPackage(RACINE + ".domain.ports..", INFRASTRUCTURE)
                .because("un contrôleur passe par un service, jamais directement par un port ou un adaptateur"));
    }

    @Test
    void les_services_du_domaine_ne_dependent_que_des_ports_et_des_modeles() {
        verifier(classes().that().resideInAPackage(RACINE + ".domain.services..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(DOMAIN, "java.."));
    }

    @Test
    void les_services_du_domaine_ne_sont_pas_des_composants_spring() {
        verifier(noClasses().that().resideInAPackage(RACINE + ".domain..")
                .should().beAnnotatedWith(org.springframework.stereotype.Component.class)
                .orShould().beAnnotatedWith(org.springframework.stereotype.Service.class)
                .because("les services du domaine sont assemblés par DomainConfig"));
    }

    @Test
    void les_ports_sont_des_interfaces() {
        verifier(classes().that().resideInAPackage(RACINE + ".domain.ports..").should().beInterfaces());
    }

    @Test
    void les_adaptateurs_realisent_un_port_du_domaine() {
        verifier(classes().that().resideInAnyPackage(RACINE + ".infrastructure.client.adapter..",
                        RACINE + ".infrastructure.persistence.adapter..", RACINE + ".infrastructure.security..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should().implement(resideInAnyPackage(RACINE + ".domain.ports..")));
    }

    @Test
    void les_ports_ne_sont_utilises_que_par_le_domaine_et_l_infrastructure() {
        verifier(classes().that().resideInAPackage(RACINE + ".domain.ports..")
                .should().onlyHaveDependentClassesThat().resideInAnyPackage(DOMAIN, INFRASTRUCTURE));
    }

    @Test
    void les_entites_jpa_restent_dans_la_couche_de_persistance() {
        verifier(classes().that().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().resideInAPackage(RACINE + ".infrastructure.persistence.entity.."));
    }

    @Test
    void les_repositories_jpa_ne_sont_utilises_que_par_les_adaptateurs_de_persistance() {
        verifier(classes().that().resideInAPackage(RACINE + ".infrastructure.persistence.repository..")
                .should().onlyHaveDependentClassesThat().resideInAnyPackage(
                        RACINE + ".infrastructure.persistence.adapter..", RACINE + ".infrastructure.persistence.repository.."));
    }

    @Test
    void le_client_http_reactif_reste_dans_l_infrastructure_des_clients_pays() {
        verifier(noClasses().that().resideOutsideOfPackages(RACINE + ".infrastructure.client..", RACINE + ".infrastructure.config..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web.reactive..", "reactor.."));
    }

    @Test
    void les_controleurs_sont_nommes_Controller_et_vivent_dans_api_controller() {
        verifier(classes().that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .should().haveSimpleNameEndingWith("Controller")
                .andShould().resideInAPackage(RACINE + ".api.controller.."));
    }

    @Test
    void les_mappers_de_l_api_ne_traduisent_que_le_domaine_et_les_dto() {
        verifier(noClasses().that().resideInAPackage(RACINE + ".api.mapper..")
                .should().dependOnClassesThat().resideInAPackage(RACINE + ".infrastructure.."));
    }
}
