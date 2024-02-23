class bank_remove (
  $tomcat_container_name = 'bank-tomcat',
  $sql_container_name = 'bank-db',
) {

  # Stop and remove Tomcat container
  exec { "stop_and_remove_${tomcat_container_name}":
    command => "/usr/bin/docker stop ${tomcat_container_name} && /usr/bin/docker rm ${tomcat_container_name}",
    onlyif  => "/usr/bin/docker ps -a --filter name=${tomcat_container_name}",
  }

  # Stop and remove SQL container
  exec { "stop_and_remove_${sql_container_name}":
    command => "/usr/bin/docker stop ${sql_container_name} && /usr/bin/docker rm ${sql_container_name}",
    onlyif  => "/usr/bin/docker ps -a --filter name=${sql_container_name}",
  }
}

# Usage:
class { 'bank_remove': }
