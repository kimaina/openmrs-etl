CREATE 
    ALGORITHM = UNDEFINED 
    DEFINER = `debezium`@`%` 
    SQL SECURITY DEFINER
VIEW `encounter_orders` AS
    SELECT 
        `orders`.`order_id` AS `order_id`,
        `orders`.`concept_id` AS `concept_id`,
        `orders`.`date_activated` AS `date_activated`,
        `orders`.`voided` AS `voided`,
        `encounter`.`encounter_id` AS `encounter_id`,
        `encounter`.`patient_id` AS `patient_id`,
        `encounter`.`encounter_type` AS `encounter_type`,
        `encounter`.`location_id` AS `location_id`,
        `encounter`.`visit_id` AS `visit_id`,
        `encounter`.`encounter_datetime` AS `encounter_datetime`,
        `person`.`gender` AS `gender`,
        `person`.`birthdate` AS `birthdate`,
        `person`.`dead` AS `dead`,
        `person`.`death_date` AS `death_date`,
        `person`.`uuid` AS `uuid`,
        `visit`.`visit_type_id` AS `visit_type_id`
    FROM
        (((`orders`
        JOIN `encounter` ON ((`encounter`.`encounter_id` = `orders`.`encounter_id`)))
        LEFT JOIN `person` ON ((`person`.`person_id` = `encounter`.`patient_id`)))
        LEFT JOIN `visit` ON ((`visit`.`visit_id` = `encounter`.`visit_id`)))
    ORDER BY `encounter`.`encounter_id`