CREATE 
    ALGORITHM = UNDEFINED 
    DEFINER = `debezium`@`%` 
    SQL SECURITY DEFINER
VIEW `obs_with_encounter` AS
    SELECT 
        `obs`.`obs_id` AS `obs_id`,
        `obs`.`concept_id` AS `concept_id`,
        `obs_2`.`concept_id` AS `parent_concept_id`,
        `encounter`.`encounter_id` AS `encounter_id`,
        `obs`.`obs_datetime` AS `obs_datetime`,
        `obs`.`obs_group_id` AS `obs_group_id`,
        `obs`.`value_coded` AS `value_coded`,
        `obs`.`value_drug` AS `value_drug`,
        `obs`.`value_datetime` AS `value_datetime`,
        `obs`.`value_numeric` AS `value_numeric`,
        `obs`.`value_modifier` AS `value_modifier`,
        `obs`.`value_text` AS `value_text`,
        `obs`.`date_created` AS `date_created`,
        `obs`.`voided` AS `voided`,
        `obs`.`person_id` AS `patient_id`,
        `encounter`.`encounter_type` AS `encounter_type`,
        `obs`.`location_id` AS `location_id`,
        `encounter`.`visit_id` AS `visit_id`,
        `encounter`.`encounter_datetime` AS `encounter_datetime`,
        `person`.`gender` AS `gender`,
        `person`.`birthdate` AS `birthdate`,
        `person`.`dead` AS `dead`,
        `person`.`death_date` AS `death_date`,
        `person`.`uuid` AS `uuid`,
        `visit`.`visit_type_id` AS `visit_type_id`
    FROM
        ((((`obs` FORCE INDEX FOR JOIN (ENCOUNTER_OBSERVATIONS)
        JOIN `encounter` ON ((`encounter`.`encounter_id` = `obs`.`encounter_id`)))
        LEFT JOIN `person` ON ((`person`.`person_id` = `obs`.`person_id`)))
        LEFT JOIN `visit` ON ((`visit`.`visit_id` = `encounter`.`visit_id`)))
        LEFT JOIN `obs` `obs_2` ON ((`obs`.`obs_group_id` = `obs_2`.`obs_id`)))
    WHERE
        (`obs`.`encounter_id` IS NOT NULL)