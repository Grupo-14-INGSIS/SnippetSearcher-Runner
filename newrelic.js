'use strict'

exports.config = {
    app_name: ['RunnerIngsis_dev'],
    license_key: '44361fb28211be08bdb208a56380f609FFFFNRAL',
    logging: {
        level: 'info'
    },
    distributed_tracing: {
        enabled: true
    },
    // Configuración para propagar request-id
    api: {
        custom_parameters_enabled: true
    },
    attributes: {
        enabled: true,
        include: ['request.headers.x-request-id']
    }
}