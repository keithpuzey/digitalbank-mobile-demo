# Define a class to manage Tomcat deployment
class tomcat_deploy (
  $docker_image = 'tomcat:latest',
  $container_name = 'dbank-host',
  $war_file_path = '/var/lib/jenkins/workspace/bank.war',
  $port_mapping = '80:8080', # Format: host_port:container_port
) {
  # Install Docker
  class { 'docker': }

  # Pull the Docker image
  docker::image { $docker_image:
    ensure => present,
  }

  # Run the Docker container
  docker::run { $container_name:
    image   => $docker_image,
    ports   => $port_mapping,
    ensure  => absent,  # Ensure container is removed
    require => Docker::Image[$docker_image],
  }

  # Copy the WAR file into the container
  exec { 'copy_war_file':
    command     => "/usr/bin/docker cp ${war_file_path} ${container_name}:/usr/local/tomcat/webapps/",
    refreshonly => true,
    require     => Docker::Run[$container_name],
  }
}

# Usage:
class { 'tomcat_deploy': }