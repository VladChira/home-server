ALTER TABLE virtual_machine
  DROP COLUMN novnc_port,
  DROP COLUMN vm_port,
  DROP COLUMN base_path,
  ADD COLUMN guac_client_id VARCHAR(255) NULL;
