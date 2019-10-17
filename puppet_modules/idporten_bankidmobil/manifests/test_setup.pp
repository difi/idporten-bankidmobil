class idporten_bankidmobil::test_setup inherits idporten_bankidmobil{

  include platform
  if ($platform::test_setup) {

    wget::fetch { 'download_bankid-keys':
      source             => 'http://static.dmz.local/vagrant/eid/resources/ID-Porten-BINAS.bid',
      destination        => "${idporten_bankidmobil::config_root}${idporten_bankidmobil::application}/ID-Porten-BINAS.bid",
      nocheckcertificate => true,
    }
  }
}