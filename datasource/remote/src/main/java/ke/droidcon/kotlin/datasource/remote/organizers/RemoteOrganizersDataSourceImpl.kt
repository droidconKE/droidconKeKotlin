package ke.droidcon.kotlin.datasource.remote.organizers

import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.organizers.model.OrganizersPagedResponse
import ke.droidcon.kotlin.datasource.remote.utils.DataResult

class RemoteOrganizersDataSourceImpl @Inject constructor(
    private val api: OrganizersApi
) : RemoteOrganizersDataSource {

    override suspend fun getIndividualOrganizers(): DataResult<OrganizersPagedResponse> =
        api.fetchOrganizers(type = "individual")

    override suspend fun getCompanyOrganizers(): DataResult<OrganizersPagedResponse> =
        api.fetchOrganizers(type = "company")
}