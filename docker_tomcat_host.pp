class bank_deploy (
  $tomcat_docker_image = 'tomcat:latest',
  $tomcat_container_name = 'bank-tomcat',
  $war_file_path = '/var/lib/jenkins/workspace/bank.war',
  $tomcat_port_mapping = '80:8080', # Format: host_port:container_port

  $sql_docker_image = 'mysql:latest',
  $sql_container_name = 'bank-db',
  $sql_root_password = 'CAdemo123',
  $sql_database = 'bank_db',
  $sql_port_mapping = '3306:3306', # Format: host_port:container_port
) {

  # Install Docker
  class { 'docker': }

  # Pull Tomcat Docker image
  docker::image { $tomcat_docker_image:
    ensure => present,
  }

  # Run Tomcat Docker container
  docker::run { $tomcat_container_name:
    image   => $tomcat_docker_image,
    ports   => $tomcat_port_mapping,
    require => Docker::Image[$tomcat_docker_image],
  }

  # Pull SQL Docker image
  docker::image { $sql_docker_image:
    ensure => present,
  }

  # Run SQL Docker container
  docker::run { $sql_container_name:
    image   => $sql_docker_image,
    ports   => $sql_port_mapping,
    env     => ["MYSQL_ROOT_PASSWORD=${sql_root_password}", "MYSQL_DATABASE=${sql_database}"],
    require => Docker::Image[$sql_docker_image],
  }

  # Copy the WAR file into the Tomcat container
  exec { 'copy_war_file':
    command     => "/usr/bin/docker cp ${war_file_path} ${tomcat_container_name}:/usr/local/tomcat/webapps/",
    refreshonly => true,
    require     => Docker::Run[$tomcat_container_name],
  }
}

# Usage:
class { 'bank_deploy': }
